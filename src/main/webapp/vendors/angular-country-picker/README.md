# angular-country-picker

AngularJS directive to generate a list of countries as options of the select element.

## Installation

You can install the directive using [Bower](http://bower.io/):

```bash
$ bower install angular-country-picker
```

Or [npm](https://www.npmjs.com/):

```bash
$ npm install angular-country-picker
```

Then you have to include it in your HTML:

```html
<script src="bower_components/angular-country-picker/country-picker.js"></script>
<script src="node_modules/angular-country-picker/country-picker.js"></script>
```

And inject the module `puigcerber.countryPicker` as a dependency of your application:

```js
angular.module('webApp', ['puigcerber.countryPicker']);
```

## Usage

The directive is intended to be used as an attribute of the native [select](https://docs.angularjs.org/api/ng/directive/select) 
directive setting its [ngOptions](https://docs.angularjs.org/api/ng/directive/ngOptions). 
Therefore `ngModel` is required for this to work.

```html
<select ng-model="selectedCountry" pvp-country-picker></select>
```

Excluding `ngOptions`, any other optional attribute of the select directive could still be used.

```html
<select name="country" ng-model="selectedCountry" pvp-country-picker ng-change="onChange()" required></select>
```

The default value to which `ngModel` is bound it's the two-letter country code, but this can be changed setting the
attribute to one of the following values:

* alpha2: two-letter country code defined in [ISO 3166-1 alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2).
* alpha3: three-letter country code defined in [ISO 3166-1 alpha-3](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3).
* numeric: three-digit country code defined in [ISO 3166-1 numeric](https://en.wikipedia.org/wiki/ISO_3166-1_numeric).
* name: the English name of the country.

```html
<select ng-model="selectedCountry" pvp-country-picker="name"></select>
```

### Config

The country provider can be configured to set a custom list of countries.

```js
angular.module('webApp', ['puigcerber.countryPicker'])
  .config(function(pvpCountriesProvider) {
    pvpCountriesProvider.setCountries([
      { name: 'Abkhazia', alpha2: 'AB'},
      { name: 'Kosovo', alpha2: 'XK'},
      { name: 'Nagorno-Karabakh', alpha2: 'NK'},
      { name: 'Northern Cyprus', alpha2: 'KK'},
      { name: 'Somaliland', alpha2: 'JS'},
      { name: 'South Ossetia', alpha2: 'XI'},
      { name: 'Transnistria', alpha2: 'PF'}
    ]);
  });
```

## See also

[ISO 3166](http://www.iso.org/iso/country_codes.htm) is the International Standard for country codes and codes for their subdivisions.
Currently 249 countries, territories, or areas of geographical interest are assigned official codes in ISO 3166-1.

## License

MIT © [Pablo Villoslada Puigcerber](http://pablovilloslada.com)
