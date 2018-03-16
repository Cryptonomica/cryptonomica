'use strict';

// see: http://julienrenaux.fr/2014/05/25/introduction-to-gulp-js-with-practical-examples/

// including plugins
var gulp = require('gulp')
, uglify = require("gulp-uglify");

// task
gulp.task('minify-js', function () {
    gulp.src('./*.js') // path to your files
    .pipe(uglify())
    .pipe(gulp.dest('./min'));
});

// gulp minify-js
