Country select for AngularJS
======================

A simple AngularJS directive to create country select. It uses [select2](http://select2.github.io/select2/) to create auto-complete country select. Country data is from [mledoze/countries](https://github.com/mledoze/countries).

### Install

Install `angular-country-select` using Bower `bower install angular-country-select`.

### Usage

Make your Angular module depend on module `angular-country-select`.

```javascript
angular.module('countrySelectExample', ['angular-country-select']);
```

Then use directive `country-select`.

```html
<input country-select data-ng-model="country">
```

Value of selected country is the [ISO 3166-1 alpha-2](http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) code, e.g. `CN`, `US`.

See `example.html` for a simple example.


### Countries data

Countries JSON data is inlined into JavaScript file during Grunt build, so no extra download is required in runtime.
