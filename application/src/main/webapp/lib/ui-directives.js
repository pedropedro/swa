/* function(s,e,a) === function($scope, $element, $attributes) */

angular.module('ui.directives', [])

/* http://angular-tips.com/blog/2013/08/why-does-angular-dot-js-rock/ */
.directive('focus', [ function() {
	return {
		link: function(s,e,a) { e[0].focus(); }
	};
}])

.directive('ngElementReady', [ function() {
	return {
		priority:	-1000, // a low number so this directive loads after all other directives have loaded.
		restrict:	"A", // attribute only
		link:		function(s,e,a) {
						console.log(" -- Element ready! ", a.ngElementReady );
						s.$eval(a.ngElementReady);
					}
	};
}])

;