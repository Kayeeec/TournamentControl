/* 
 * @author Karolina Bozkova
 */

(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EvaluationTableController', EvaluationTableController);

    EvaluationTableController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState'];

    function EvaluationTableController($scope, $rootScope, $stateParams, previousState) {
        var vm = this;

        vm.previousState = previousState.name;
        
        
        
        
        

        var unsubscribe = $rootScope.$on('tournamentControlApp:participantUpdate', function(event, result) {
            vm.participant = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        
    }
})();


