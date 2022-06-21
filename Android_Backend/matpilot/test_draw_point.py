# -*- coding: UTF-8 -*-
import matplotlib.pyplot as plt

import random
from matplotlib.widgets import Button

# Draw multiple points.
def draw_multiple_points():
    # x axis value list.
    x_number_list = [1, 4, 9, 16, 25]
    # y axis value list.
    y_number_list = [1, 2, 3, 4, 5]
    # Draw point based on above x, y axis values.
    # plt.scatter(x_number_list, y_number_list, s=2)
    # 2 list for x y, last 's' is point size
    # plt.scatter(y_number_list, x_number_list, s=10)

    # Set chart title.
    plt.title("Extract Number Root ")
    # Set x, y label text.
    plt.xlabel("Number")
    plt.ylabel("Extract Root of Number")
    plt.show()


def refresh():
    # get figure, 
    # draw for refresh
    fig = plt.gcf()
    fig.canvas.draw()

def onclick(event):
    if not hasattr(event, "xdata"):
        return
    if not hasattr(event, "ydata"):
        return
    if (event.xdata is None or event.ydata is None):
        return
    print('%s click: button=%d, x=%d, y=%d, xdata=%f, ydata=%f' % ('double' if event.dblclick else 'single', event.button, event.x, event.y, event.xdata, event.ydata))
    plt.scatter([event.xdata], [event.ydata], s=2)
    refresh()
    

def draw_line():
    x1, y1 = [10000, 1000], [30000, 10000]
    x2, y2 = [20000, 2000], [100, 30000]
    # draw pairs of points to be a line
    plt.plot(x1, y1, x2, y2, marker = 'o')
    x3, y3 = [40000, 1000], [60000, 10000]
    plt.plot(x3, y3, marker = 'o')
    refresh()


# set x, y range to fix number
def fix_x_y_range(x_num, y_num):
    plt.xlim([0, x_num])
    plt.ylim([0, y_num])

#
def random_generate_points(num, x_max, y_max):
    x_list = [random.random() * x_max for i in xrange(0, num)]
    y_list = [random.random() * y_max for i in xrange(0, num)]
    plt.scatter(x_list, y_list, s=2)

def bind_click_evnt():

    fig = plt.gcf()
    cid = fig.canvas.mpl_connect('button_press_event', onclick)



if __name__ == '__main__':
    x_max = 100000
    y_max = 100000
    fix_x_y_range(x_max, y_max)

    random_point_count = 100
    random_generate_points(random_point_count, x_max, y_max)
    # simple draw some point

    # bind click event
    bind_click_evnt()

    # draw line
    draw_line()

    # add_button_and_bind_event()

    draw_multiple_points()

    # random generate some point and show


