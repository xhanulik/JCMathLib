# 0. Install the DifFuzz tool according to the instructions in
#    https://github.com/isstac/diffuzz/blob/master/README.md
#    put Kelinci JAR and JCardSim JAR into JCMathLib/libs/

# 1. build the JCMathLib library with gradle, omit running tests
./gradlew clean build -x test

# 2. setup paths for Kelinci JAR and JCardSim JAR
export KELINCI=/path/to/JCMathLib/libs/kelinci.jar
export JCARDSIM=/path/to/libs/jcardsim-3.0.6.0.jar

# 3. navigate into the directory with compiled .class files
cd applet/build/classes/java

# 4. instrument the classes with Kelinci Instrumentor
#    - .class files are in main/ directory
#    - instrumented binaries are in bin-instr/ directory
java -cp $KELINCI edu.cmu.sv.kelinci.instrumentor.Instrumentor -i main/ -o bin-instr -skipmain

# 5. prepare the in/ and out/ directories and the seed corpus for the fuzzing in out/ directory
mkdir in out
touch in/testcase

# 6. test the driver with one input
java -cp bin-instr:$JCARDSIM  DifFuzzDriver in/testcase

# 7. start Kelinci server
#    WARNING: the Kelinci server requires separate window for launching and running
java -cp bin-instr:$JCARDSIM edu.cmu.sv.kelinci.Kelinci DifFuzzDriver @@

# 8. continue with setting up the AFL fuzzer in diffuzz_run_fuzzing.sh in separate window
