class Hello:
    __gui = None

    def __init__(self, gui):
        self.__gui = gui

    def run(self, name):
        print 'Hello ' + name + '!'