# The ola client that drives the various lounge lighting


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

# Questions:
# - client vs wrapper?

import array
from ola.ClientWrapper import ClientWrapper

wrapper = None
mapper = None
loop_count = 0
TICK_INTERVAL = 100  # in ms

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
        self.channels[idx] = color.r()
        self.channels[idx + 1] = color.g()
        self.channels[idx + 2] = color.b()


class DmxMapper:
    """
    Maps the lounge layout to a DMX universe.

    Used to generate next DMX data frame from the current lounge state.
    """

    def __init__(self, universe = DEFAULT_UNIVERSE, fixtures = []):
        self.universe = universe
        self.fixtures = fixtures
        
    
    # XXX get universe too
    def get_data(self):
        # XXX more efficient pre-alloc / pre-calc len
        data = array.array('B')
        for f in self.fixtures:
            data.extend(f.channels)
        
        return (self.universe, data)


class FadeDriver:
    # black - white - black
    # 1 channel at a time
    # treat both chandeliers as linked

    def __init__(self):
        # XXX hard coded mapping (cow slices instead?)
        self.fixtures = [Chandelier(), Chandelier()]
        self.current_attr_num = 0
        self.start_color = COLOR_BLACK
        self.target_color = COLOR_WHITE
        self.current_color = self.start_color


    def step(self):
        # XXX pass in the timecode / deadline instead?
        # XXX not 255 even steps for all colors?

        if self.current_color == COLOR_WHITE:
            return

        # XXX correct lerp / cap at target
        self.current_color = Color(
            self.current_color.r() + 1, 
            self.current_color.g() + 1,
            self.current_color.b() + 1)

        # simulate both chandeliers locked
        self.fixtures[0].set_color(0, self.current_color)
        self.fixtures[1].set_color(0, self.current_color)
        

class DMXController:
    
    def __init__(self, driver):
        self.driver = driver


    # XXX clean up the dmx sending into a controller
    def DmxSent(self, state):
        if not state.Succeeded():
            wrapper.Stop()


    def SendDMXFrame(self):
        # schdule a function call in 100ms
        # we do this first in case the frame computation takes a long time.

        # XXX change this to be a time-space parametric fn
        # XXX this might stomp over / do deadlines instead
        wrapper.AddEvent(TICK_INTERVAL, self.SendDMXFrame)

        # # compute frame here
        # dmx_data = array.array('B')
        # global loop_count
        # dmx_data.extend([loop_count, loop_count, loop_count])
        # loop_count = (loop_count + 1) % 255

        # XXX do this out of band
        self.driver.step()

        # XXX odd state sharing :/
        universe, data = mapper.get_data()
        wrapper.Client().SendDmx(universe, data, self.DmxSent)


driver = FadeDriver()
mapper = DmxMapper(DEFAULT_UNIVERSE, driver.fixtures)

# XXX wrapper vs dmx controller
dmx = DMXController(driver)
wrapper = ClientWrapper()
wrapper.AddEvent(TICK_INTERVAL, dmx.SendDMXFrame)
wrapper.Run()
