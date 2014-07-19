#!/usr/bin/env python

import Gnuplot
import sys
import math


def plot():
  for termext in terminalExtensions:

    g = Gnuplot.Gnuplot()
    g._clear_queue()
    g('set xlabel "protocol and cycles"')
    g('set key on')
    g('set key left top')
    #g('set logscale xy')
    g('set ylabel "estimate"')
    plotfile = "netsize." + termext

    for nodes in range(1000,1100,1):
      datafile = 'aggr_%i.dat' % nodes
      legend = 'nodes=%d, ' % nodes
      g._add_to_queue([Gnuplot.File(datafile, using='9', with_='linespoints', title=None)])  # '%i nodes'%nodes)])

      if termext=='eps':
        g('set size 0.6,0.6')
        g.hardcopy(plotfile, 'postscript', enhanced=True, color=True)
      elif termext=='png' or termext=='svg':
        g.hardcopy(plotfile, termext)



    for nodes in range(1000, 1100, 1):
      g = Gnuplot.Gnuplot()
      g._clear_queue()
      g('set xlabel "protocol and cycles"')
      g('set key on')
      g('set key left top')
      g('set ylabel "estimate"')
      g('set yrange [0:12000]')
      plotfile = "netsize_n0%i." % nodes + termext

      datafile = 'aggr_%i.dat' % nodes
      legend = 'nodes=%d, ' % nodes
      g._add_to_queue([Gnuplot.File(datafile, using='5', with_='linespoints', title=None)])  # '%i nodes'%nodes)])

      if termext=='eps':
        g('set size 0.6,0.6')
        g.hardcopy(plotfile, 'postscript', enhanced=True, color=True)
      elif termext=='png' or termext=='svg':
        g.hardcopy(plotfile, termext)





terminalExtensions = ('png', 'eps')

plot()
