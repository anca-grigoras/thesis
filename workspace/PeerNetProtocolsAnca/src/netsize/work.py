#!/usr/bin/env python


def filename(nodes, algorithm):
  str = 'ns'
  str += '_n%05i' % (nodes)
  str += '_alg:%s' % algorithm
  return str

def make_work():

  r=0

  for nodes in range(1000, 1100, 1):
  #for nodes in range(10000,100000,5000):
    #for algorithm in ('myMinTopK', 'minTopK', 'pc', 'pcsa'):
    for algorithm in ('ams','bjkst2','linear_counting','minTopK','mrb','mrb_linked_list','myMinTopK','pc','pcsa','pcsa_bitset'):
      #for (parameters, filename) in getParameters(algorithm):
        
            outf = filename(nodes, algorithm)

            cmd = ''
            cmd += 'cd Source/PeerEmuProtocols; '
            cmd += './scripts/go ./src/netsize/netsize.cfg '
            cmd += 'NODES=%i ' % nodes
            cmd += 'protocol.netsize.algorithm=%s ' % algorithm
            cmd += 'random.seed=%s ' % r
            cmd += '> %s.dat 2> %s.err' % (outf, outf)

            print cmd
            r += 1

make_work()
