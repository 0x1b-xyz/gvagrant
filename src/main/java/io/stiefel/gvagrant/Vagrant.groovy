/**
 * The MIT License (MIT)

 Copyright (c) 2013, Jason Stiefel

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package io.stiefel.gvagrant

import groovy.util.logging.Log

import java.util.logging.Level
import java.util.regex.Pattern

/**
 * Uses the Groovy {@link AntBuilder} to manage a {@code vagrant} command line process.
 *
 * @author Jason Stiefel <jason@stiefel.io>
 */
@Log
class Vagrant {

    private final static Pattern STATUS_PATTERN = ~/[^\s]+?\s{3,}+(.*)\((\w++)\)/


    final File vagrantFile
    private final AntBuilder ant = new AntBuilder().with {
        it.project.buildListeners.firstElement().emacsMode = true
        it
    }

    Vagrant(File vagrantFile) {
        this.vagrantFile = vagrantFile
        if (!vagrantFile.exists())
            throw new FileNotFoundException(vagrantFile.absolutePath);
    }

    void up() {
        log.info("Running `up` on ${vagrantFile.absolutePath}")
        vagrantExec {
            arg(line: 'up')
        }
    }

    void halt() {
        log.info("Running `halt` on ${vagrantFile.absolutePath}")
        vagrantExec {
            arg(line: 'halt')
        }
    }

    void destroy() {
        log.info("Running `destroy` on ${vagrantFile.absolutePath}")
        vagrantExec {
            arg(line: 'destroy')
        }
    }

    /**
     * Runs the {@code vagrant status} command and parses the output to return a {@link Status}. Currently supports
     * 1 box - parses only the first line.
     */
    Status status() {

        log.info("Running `status` on ${vagrantFile.absolutePath}")

        def output = vagrantExec {
            arg(line: 'status')
        }

        def status = output.readLines().grep(STATUS_PATTERN)?.first().with {
            (it =~ STATUS_PATTERN).with {
                if (!it.matches())
                    return null
                it.group(1).trim()
            }
        }

        if (!status)
            throw new IllegalStateException("Could not determine status from output:\n\n${output}")

        Status.values().find { it.message.equals(status) }

    }

    /**
     * Runs the {@code vagrant} command via an Ant {@code exec} task. Unfortunately vagrant doesn't have an option to
     * return a meaningful exit status so we're left with parsing the command output.
     *
     * @param exec Closure that builds the Ant {@code exec} task
     * @return Output of the command.
     */
    protected String vagrantExec(Closure exec) {

        Closure builder = exec.clone()
        builder.resolveStrategy = Closure.DELEGATE_ONLY
        ant.exec(executable: 'vagrant', failonerror: true,
                dir: vagrantFile.parentFile,
                outputproperty: "cmdOutput",
                resultproperty: "cmdExit") {
            builder.delegate = delegate
            builder()
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Vagrant.exec output:\n ${ant.project.properties['cmdOutput']}")
            log.fine("Vagrant.exec exit code: ${ant.project.properties['cmdExit']}")
        }

        return ant.project.properties['cmdOutput']

    }

    static enum Status {
        NOT_CREATED("not created"), POWEROFF("poweroff"), RUNNING("running")
        final String message
        Status(String message) {
            this.message = message
        }
    }

}
