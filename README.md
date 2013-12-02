gvagrant
========

Prototype Groovy wrapper for the Vagrant command line

What This Is
------------

A simple convenience wrapper that executes the `vagrant` command using the
    [Ant Exec Task](https://ant.apache.org/manual/Tasks/exec.html) via the
    [Groovy AntBuilder](http://groovy.codehaus.org/api/groovy/util/AntBuilder.html).

What This is Not
----------------

An exhaustive tool for executing `vagrant`. I'm purpose building this to use from a Maven plugin, so there's not
    much in the way of tests beyond my simple use cases.

How To Use
----------

***Prerequisites:***
- `vagrant` In your path
- `virtualbox` In your path

```Vagrant v = new Vagrant(new File('/path/to/my/Vagrantfile'));
assert v.status() == Vagrant.Status.POWERED_OFF
v.up();
```
