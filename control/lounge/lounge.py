#!/usr/bin/env python

# The ola client that drives the various lounge lighting.


# TODO robust re-connect  (or just restart the client?)
# TODO driver logging
# TODO touch OSC server (receives events)
# TODO automate rPi config / install
#       - install ola
#       - pip install six


# TODO Setup control (patch unpatch etc) vs light show
# TODO debug mode / locate mode

# TODO basic chandelier fixture
# TODO color interp


from __future__ import division # floating point division

import array
import random
from sys import path
from time import sleep
import types

from ola.OlaClient import OlaClient
# from ola.ClientWrapper import ClientWrapper
import OSC

def clamp(n, smallest, largest):
    return max(smallest, min(n, largest))


# start color
# target color
# current color

DEFAULT_UNIVERSE = 1

class Color:
    """8-bit RGB"""

    def __init__(self, r, g, b):
        self.rgb = (r,g,b)

    def __eq__(self, other):
        return self.rgb == other.rgb

    def r(self):
        return self.rgb[0]

    def g(self):
        return self.rgb[1]

    def b(self):
        return self.rgb[2]

    def __str__(self):
        return "rgb%s" % str(self.rgb)

    @staticmethod
    def lerpRGB(a, b, t):
        # XXX do this better, linear rgb interpolation sucks
        # XXX cap / max
        # XXX check t is [0..1]
        return Color(
            a.r() + int((b.r() - a.r()) * t),
            a.g() + int((b.g() - a.g()) * t),
            a.b() + int((b.b() - a.b()) * t))


COLOR_BLACK = Color(0,0,0)
COLOR_WHITE = Color(255,255,255)


class Chandelier:
    # XXX gross hacks / hard mapped as RGB
    NUM_COLORS = 4
    # XXX color -> channel abstractions
    def __init__(self):
        self.channels = [0,0,0,  0,0,0,  0,0,0, 0,0,0]
    
    # XXX chandeliers should be addressable by color
    # e.g c[0] = Color(r,g,b)
    def set_color(self, idx, color):
        # XXX check idx less than num_colors
        offset = 3*idx
        self.channels[offset] = color.r()
        self.channels[offset + 1] = color.g()
        self.channels[offset + 2] = color.b()

    def __str__(self):
        return "Ch %s" % str(self.channels)


class DmxMapper:
    """
    Maps the lounge layout to a DMX universe.

    Used to generate next DMX data frame from the current lounge state.
    """

    def __init__(self, fixtures, universe = DEFAULT_UNIVERSE):
        self.universe = universe
        self.fixtures = fixtures
        
    
    # XXX get universe too
    def get_data(self):
        # XXX more efficient pre-alloc / pre-calc len
        data = array.array('B')
        for f in self.fixtures:
            data.extend(f.channels)
        
        return (self.universe, data)


# oneshot vs cyclic drivers?

class ColorFadeDriver:
    # XXX not sure how to strucutre this. Does this even need state?
    # I think it's useful to not make this a fn and let each 
    # driver have their own step / num steps scale

    def __init__(self, source_color, target_color, num_steps = 255):
        self.source_color = source_color
        self.target_color = target_color
        self.num_steps = num_steps
        # XXX interpolation style

        self._current_step = 0
        self._current_color = self.source_color


    def step(self):
        # XXX pass in the timecode / deadline instead?
        if not self.is_done():
            lerp_t = clamp(self._current_step / self.num_steps, 0, 1)
            self._current_color = Color.lerpRGB(self.source_color, self.target_color, lerp_t)
            self._current_step += 1


    def is_done(self):
        # XXX check arg never greater than?
        return self._current_step == self.num_steps

    def output(self):
        """Outputs a single color"""

        return self._current_color
        

class ColorPalletFadeDriver:
    # XXX this one loops / not 1 shot. How to tell them apart?

        # XXX start mode is all black to first palette, then once at a time
        # self.initial_source_color = COLOR_BLACK


    def __init__(self, color_palette, num_output_colors):
        # XXX check num output < len(color_palette)
        
        self.source_color_palette = color_palette

        self.current_colors = self.source_color_palette[:num_output_colors]
        self._setup_fader()


    def _setup_fader(self):
        # XXX multiple concurrent
        # XXX not random, favor unasigned colors and another color index
        self.current_changing_idx = random.randint(0, len(self.current_colors) - 1)

        self.color_fader = ColorFadeDriver(
            self.current_colors[self.current_changing_idx],
            random.choice(self.source_color_palette))


    def step(self):
        self.color_fader.step()
        self.current_colors[self.current_changing_idx] = self.color_fader.output()

        # TODO control verbosity
        #print ", ".join([str(c) for c in self.current_colors])
        if self.color_fader.is_done():
            self._setup_fader()

    def is_done(self):
        return False
    
    def output(self):
        return self.current_colors


class DMXController:
    
    def __init__(self, ola_client, mapper, driver, fixtures):
        self.ola_client = ola_client
        # XXX rename driver, confusing
        self.mapper = mapper
        self.driver = driver
        self.fixtures = fixtures


    # XXX clean up the dmx sending into a controller
    def DmxSent(self, state):
        if not state.Succeeded():
            pass
            # XXX bubble up to driver
            # wrapper.Stop()


    def SendDMXFrame(self):
        # schdule a function call in 100ms
        # we do this first in case the frame computation takes a long time.

        # XXX change this to be a time-space parametric fn
        # XXX this might stomp over / do deadlines instead
        # wrapper.AddEvent(TICK_INTERVAL, self.SendDMXFrame)

        # # compute frame here
        # dmx_data = array.array('B')
        # global loop_count
        # dmx_data.extend([loop_count, loop_count, loop_count])
        # loop_count = (loop_count + 1) % 255

        # XXX do this out of band
        self.driver.step()

        # XXX hard coded driver output to fixtures
        for f in self.fixtures:
            for idx, c in enumerate(self.driver.output()):
                f.set_color(idx, c)

        # XXX odd state sharing :/
        universe, data = self.mapper.get_data()
        self.ola_client.SendDmx(universe, data, self.DmxSent)


class OSCServer:
    """Wrapper around a pyosc OSC Server.

    Handles coorperative pre-emption with scene animation run-loop.

    Handlers must all be fast, and just update scene state to return
    to the run loop.

    TODO just make this threaded
    """
    
    def __init__(self):
        self.timed_out = False
        addr = ("0.0.0.0", 8000)
        self.server = OSC.OSCServer(addr)
        self.server.timeout = 0
        self.server.handle_timeout = self.handle_timeout
        
        self.server.addMsgHandler("/palette/1/1", self.change_color_palette_handler)
        self.server.addMsgHandler("/palette/1/2", self.change_color_palette_handler)
        self.server.addMsgHandler("/palette/1/3", self.change_color_palette_handler)
        # self.server.addMsgHandler("/palette/1/4", self.change_color_palette_handler)
        # self.server.addMsgHandler("/palette/1/5", self.change_color_palette_handler)
        # self.server.addMsgHandler("/palette/1/6", self.change_color_palette_handler)


        # color pallet index 0-2
        self.value = 0

    def close(self):
        self.server.close()

    def handle_timeout(self):
        self.timed_out = True

    def handle_available_requests(self):
        """Handles any avaiable requests.

        Returns when no requests are available
        TODO currently allows clients to continuously send 
        requests and prevent a timely return to the render loop.
        """

        self.timed_out = False
        self.value = None
        # handle all pending requests then return
        while not self.timed_out:
            self.server.handle_request()
        
        # XXX clean this up / use a queue
        return self.value


    def change_color_palette_handler(self, path, tags, args, source):
        if args[0] == 0.0:
            # ignore the toggle off. Toggle on even will follow
            return

        idx = path.split("/")[-1]
        self.value = int(idx) - 1

class Driver:
    """Drives the animation.

    Handles incoming OSC client requests to update the scene.
    Renders and sends out DMX data.
    """

    #mapper = None
    TICK_INTERVAL = 100  # in ms

    
    def __init__(self):
        self.osc_server = OSCServer()

        # Setup the scene
        # XXX do this better
        self.palettes = [
            [# Acid Sunset
            Color(27, 118, 160),
            Color(202, 115, 178),
            Color(198, 170, 208),
            Color(226, 190, 210),
            Color(242, 209, 208)],

            [# Carina Nebula
            Color(193, 89, 112),
            Color(226, 169, 107),
            Color(188, 136, 104),
            Color(27, 47, 71),
            Color(44, 102,109),
            Color(92, 151, 127),],

            [# Ocean Sunrise
            Color(163, 202, 222),
            Color(174, 212, 211),
            Color(245, 222, 218),
            Color(246, 185, 190),
            Color(44, 102,109),
            Color(219, 228, 238),]
        ]

        self.fixtures = [Chandelier(), Chandelier()]
        self.driver = ColorPalletFadeDriver(self.palettes[0], 4)
        self.mapper = DmxMapper(self.fixtures, DEFAULT_UNIVERSE)
        self.dmx = DMXController(OlaClient(), self.mapper, self.driver, self.fixtures)


    def run(self):
        while True:
            next_palette = self.osc_server.handle_available_requests()
            if next_palette is not None:
                print "changing to palette: %d" % next_palette
                self.driver = ColorPalletFadeDriver(self.palettes[next_palette], 4)
                self.dmx.driver = self.driver

            self.dmx.SendDMXFrame()

            # XXX decouple render rate from frame rate 
            #sleep(0.010)
            #sleep(0.100)
            sleep(0.001)
            # sleep(TICK_INTERVAL) # XXX sleep until next render dealine instead
        
        self.osc_server.close()


if __name__ == "__main__":
    Driver().run()
