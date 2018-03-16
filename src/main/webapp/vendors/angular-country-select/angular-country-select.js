angular.module('angular-country-select', [])
  .directive('countrySelect', [function() {
    return {
      restrict: 'A',
      require:'ngModel',
      link: function(scope, elem, attrs, ngModelCtrl) {
        var data = [];
        elem.select2({
          data: data
        });
      }
    };
  }]);
