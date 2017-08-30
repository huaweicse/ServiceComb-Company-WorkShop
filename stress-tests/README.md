# Steps to stress tests *Company Demo* using JMeter

1. make sure the *Company Demo* is up
2. replace host and port information in `hosts.csv` file with the working *Company Demo*'s.
3. run JMeter tests
```bash
jmeter -n -t workshop.jmx -j workshop.log -l workshop.jtl -Jthreads=100 -Jduration=600
```
The threads parameter means the simulated concurrency while the duration means the test period.

After the tests finished, you can check the result in jmeter's user interface by calling `jmeter` directly and import the *workshop.jmx* file.
