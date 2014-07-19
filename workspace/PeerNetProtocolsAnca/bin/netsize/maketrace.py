#!/usr/bin/env python
(X,Y) = (128,8)
(X,Y) = (3,2)
NODES = X*Y
ROUNDS = 600
ROUNDS = 6

def nodeId(x,y):
  return x+y*X

def coords(id):
  return (id%X,id/X)

def inferNeighbors(id):
  (x,y) = coords(id)
  neighbors = []
  if x>0:
    neighbors.append(nodeId(x-1,y))
  if x<X-1:
    neighbors.append(nodeId(x+1,y))
  if y>0:
    neighbors.append(nodeId(x,y-1))
  if y<Y-1:
    neighbors.append(nodeId(x,y+1))
  return neighbors



neighbors = [None] * NODES
for id in range(NODES):
  neighbors[id] = inferNeighbors(id)
  #print 'Node:', id, 'neighbors', neighbors[id]

print '%i\t%i' % (NODES, ROUNDS)

for round in range(ROUNDS):
  for node in range(NODES):
    for neighbor in neighbors[node]:
      #time = round + random.random()
      print '%i\t%i\t%i' % (round, node, neighbor)
