/**
 * Test for Comparing two software version numbers (e.g. 1.7.1)
 * Returns:
 *
 *  0 if they're identical
 *  negative if v1 < v2
 *  positive if v1 > v2
 *  Nan if they in the wrong format
 */

module("Excellentable version compare");
test('Wrong format ', function () {
    equal(compareVersionNumbers("1.7", "1..7"), NaN);
    equal(compareVersionNumbers("1.7", "Bad"), NaN);
    equal(compareVersionNumbers("1..7", "1.7"), NaN);
    equal(compareVersionNumbers("Bad", "1.7"), NaN);
});

test('Equal section', function () {
    equal(compareVersionNumbers("1.7.10", "1.7.10"), 0);
    equal(compareVersionNumbers("1.7", "1.7"), 0);
});

test('Positive section', function () {
    equal(compareVersionNumbers("1.7.10", "1.7.1"), 1);
    equal(compareVersionNumbers("1.7.10", "1.6.1"), 1);
    equal(compareVersionNumbers("1.7.10", "1.6.20"), 1);
    equal(compareVersionNumbers("1.7.0", "1.7"), 1);
    equal(compareVersionNumbers("1.8.0", "1.7"), 1);
});

test('Negative section', function () {
    equal(compareVersionNumbers("1.7.1", "1.7.10"), -1);
    equal(compareVersionNumbers("1.6.1", "1.7.10"), -1);
    equal(compareVersionNumbers("1.6.20", "1.7.10"), -1);
    equal(compareVersionNumbers("1.7.1", "1.7.10"), -1);
    equal(compareVersionNumbers("1.7", "1.7.0"), -1);
    equal(compareVersionNumbers("1.7", "1.8.0"), -1);
});
