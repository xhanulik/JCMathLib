# 0. prepare the Kelinci server in separate window with diffuzz_run_fuzzing.sh

# 1. navigate to aflf-fuzz location
cd diffuzz/tool/afl-2.51b-wca

# 2. test Kelinci interface with some seed input
../../diffuzz/tool/fuzzerside/interface $IN/testcase

# 3. run AFL fuzzer with Kelinci interface and seed corpus
./afl-fuzz -i $IN -o $OUT ../../diffuzz/tool/fuzzerside/interface @@

# 4. stop fuzzing with CTRL+C
# 5. assess results in $OUT directory
