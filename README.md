# lrudiskcache
Disk based LRU Cache

This project is very much a WIP


This is a thread safe implementation of LRU Cache where cache size is bound by two parameters
1. ```valueCount```: It bounds the number of nodes allowed in the in-memory data structure.
2. ```maxSize```: It bounds the max storage size in directory in disk.

#### Storage Structure
The in-memory storage structure uses ```LinkedHashMap``` where *key* is a String and *value* is a *Node* with fields
- fileName: A string representing *filename* containing data.
- dataSize: A long storing size of data.

The actual data is a byte array, stored on disk.

### TODO
1. Add a *journal* file to directory structure which stores the in-memory data structure, and can be used for initialization
of in-memory data structure.
2. Add a constructor which clears the storage directory on start-up.
