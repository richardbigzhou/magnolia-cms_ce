// Checks for jars with same name but different versions
// and prints them out.

def exceptions = ["antlr", "icepush"]

def directory = "."
try {
    if (args.length > 0)
        directory = args[0]
} catch (MissingPropertyException e) {
}

try{
    if (project.properties["libdir"])
        directory = project.properties["libdir"]
} catch (MissingPropertyException e) {
}

def jarMap = [:]
def collision = false
new File(directory).eachFile() { file ->
    if (file.name.endsWith("jar")) {
        def matcher = (file.name =~ /(.+)-[0-9\._]+(-SNAPSHOT|-asl)?\.jar/)
        if (matcher.matches()) {
            def artifact = matcher[0][1]

            if (jarMap.containsKey(artifact)) {
                if (exceptions.contains(artifact)) {
                    print "Expected "
                } else {
                    collision = true
                }
                println String.format("collision: %s", [file.name] + jarMap[artifact])
            } else {
                jarMap[artifact] = []
            }
            jarMap[artifact].add(file.name)
        } else {
            println String.format("WARNING: couldn't parse '%s'", file.name)
        }
    }
}

if (collision) {
    throw new RuntimeException("Collisions found.")
}
