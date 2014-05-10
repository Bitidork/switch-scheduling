switch-scheduling
=================
A project used to verify results of two scheduling algorithms outlined in High-speed Switch Scheduling for Local Area Networks by Anderson, Owicki, Saxe, and Thacker (1992): Parallel-Iterative Matching and Statistical Matching.

This project implements these schedulers in ParallelScheduler.java and StatisticalScheduler.java, respectively.

There are two simulations in the default package. 
MaximalIterationsTest.java: Runs over a variety of switch sizes to verify that, on average, one will need only log(N) iterations of parallel-iterative matching to yield a maximal matching. Outputs the matching data to data/maximal_testing.m.
Simulator.java: Simulates the two types of testing networks supplied, over FIFO, Parallel-Iterative, and Statistical Matching scheduling algorithms, and prints their performance.

The two types of networks involve N generators, N receivers, and a single N by N switch. A generator only sends messages out to a receiver. A receiver only processes messages.
The networks:
  1. Uniform Network: The amount of data per frame that any two generators send to any receiver is the same. All receivers see the same incoming flow.
  2. Privileged Generator Network: The least-privileged generator sends p data per frame to all receivers. The next-most privileged generator sends 2p data per frame to all receivers, ...etc. All receivers see the same incoming flow.
  
The constant Constants.SAFE_CAPACITY restricts the amount of data per frame going through the links between the switch and the receiver.

To create a scheduler, simply extend and implement the VOQScheduler or Scheduler class and add it as one of the schedulers to use in Simulator.java.
