/*
 * This file Copyright (c) 2010-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */

def findFiles(startPath, name) {
    def filesFound = []
    def directory = new File(startPath)
    if (directory.isDirectory()) {
        def findFileClosure = {
            if (name.matcher(it.name).find()) {
                filesFound << it
            }
        }
        directory.eachFileRecurse(findFileClosure)
    }
    return filesFound
}

class LogReader {
    
    private num_lines
    private num_errors

    LogReader(File log) {

        num_lines = num_errors = 0

        log.eachLine { line ->
            checkForErrors(line)
            num_lines += 1
        }
    }

    def checkForErrors(line) {
        if ( (line ==~ /^ERROR.*$/) ||
             (line ==~ /.*RenderException.*/) ) {
            announceError(line)
        }
    }

    def getErrors() {
        return num_errors
    }

    def announceError(line) {
        println line
        num_errors += 1
    }
}


def path

try {
    path = args.first()
} catch (MissingPropertyException) { //We're probably running from maven
    path = project.properties["logspath"]
}

File logdir = new File(path)
if (!logdir.exists()) {
    System.err.println("${path}: No such file or directory.")
    throw new RuntimeException("No such file or directory.")
}


def files = []

if (!logdir.isDirectory()) {
    files << logdir
} else {
    files = findFiles(logdir.absolutePath, ~/magnolia.*.log/)
}

def totalErrors = 0

files.each { file ->
    println "="*40
    println "Parsing log ${file.path}"
    println "="*40
    
    def parser = new LogReader(file)
    def errors = parser.getErrors()
    totalErrors += errors

    println "${errors} errors!"
    println ""
}

if (totalErrors != 0)
    throw new RuntimeException("Errors found while parsing the log files.")
