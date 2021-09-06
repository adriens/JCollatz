## Put the data in

```
MATCH (n)
DETACH DELETE n;
```

Then load some data :

First the nodes :

```
LOAD CSV WITH HEADERS FROM 'file:///NODES.csv' AS line
CREATE (:Number {
     id: line.id
    });
MATCH (n:Number)
RETURN n;
```
Add unicy constraint :

```
CREATE CONSTRAINT uniqueNumber IF NOT EXISTS
ON (n:Number)
ASSERT n.id IS UNIQUE;
```

Finally the edges :

```
LOAD CSV WITH HEADERS FROM "file:///RELATIONS.csv" AS row
MATCH (i:Number), (j:Number)
WHERE
    i.id = row.source AND
    j.id = row.target
MERGE (i)-[r:LINKED_TO]->(j)
ON CREATE SET r.weight = 1
ON MATCH SET r.weight = r.weight + 1
RETURN r;
```

## Reporting

Now is the fun part.

## Detect subgraphs


///////////////////////////////////////////////////////////////////:
// Subraphs
CALL gds.graph.create(
  'Collatz',
  'Number',
  'LINKED_TO',
  {
    relationshipProperties: 'weight'
  }
);

// count subgraphs
CALL gds.wcc.stats('Collatz')
YIELD componentCount;

// tag components
CALL gds.wcc.mutate('Collatz', { mutateProperty: 'componentId' })
YIELD nodePropertiesWritten, componentCount;

// put compoentId on each node to flag them as part of the same component
CALL gds.wcc.write('Collatz', { writeProperty: 'componentId' })
YIELD nodePropertiesWritten, componentCount;

///////////////////////////////////////////////////////////////////
// Local clustering coefficient, add localClusteringCoefficient
CALL gds.graph.create(
  'ClusterCoeff',
  'Number',
  {
    LINKED_TO: {
      orientation: 'UNDIRECTED'
    }
  }
);
CALL gds.localClusteringCoefficient.write('ClusterCoeff', {
  writeProperty: 'localClusteringCoefficient'
})
YIELD averageClusteringCoefficient, nodeCount;

///////////////////////////////////////////////////////////////////////
// PageRank
CALL gds.graph.create(
  'pageRank',
  'Number',
  'LINKED_TO',
  {
    relationshipProperties: 'weight'
  }
);

CALL gds.pageRank.write('pageRank', {
  maxIterations: 20,
  dampingFactor: 0.85,
  writeProperty: 'pagerank'
})
YIELD nodePropertiesWritten, ranIterations;

///////////////////////////////////////////////////////////////////////
// diferent compoentId from positive nodes
MATCH (n:Number)
    where toInteger(n.id) >= 0
RETURN DISTINCT n.componentId, count(*);
// see negative numbers
MATCH (n:Number)
    where toInteger(n.id) < 0
RETURN DISTINCT n.componentId, count(*);

// numbers the most heavily linked together
MATCH (i:Number)-[r:LINKED_TO]->(j:Number)
    //where toInteger(n.id) >= 0
RETURN r.weight,i,j
order by r.weight desc limit 100;

## Detect loops


## Detect bifuractions

Inject Indegree and Outdegree

## size of the biggest component

// number of nodes by componentid