# Ripple Topology Library

## Background and terminology
A **topology** is the arrangement of elements in a network.

An **element** is the smallest unit in a topology. An element must have a lifecycle and/or act as a resource.

An element that is a **resource** may be fetched from the topology by a key. This allows the element
to be inspected throughout the course of its lifecycle (if it has one).

An element that has a **lifecycle** may be started and stopped within the context of a topology.

An element may also be aware of the topology in which it lives. In this case, it may execute tasks
after starting or stopping, perhaps to configure other elements.

Elements may be started sequentially, or in parallel. Elements may be started individually, or grouped
into collections and started in groups.

Some concrete examples of elements are containerized pieces of software (applications, databases, etc),
and configurers (which configure other elements in the topology by setting environment variables
or runtime parameters).

## Usage
When run as a server, this topology service accepts topologies specified as "text/yaml". The topology
specification is a complete description of the topology, including what elements should be included,
as well as how these elements should be configured. For an example tpology yaml file, see
[here](ripple-topology-elements-nomad/src/test/resources/topology-sf-ny.yaml). The topology service will produce a fully functional network topology,
according to the specification in the yaml file.