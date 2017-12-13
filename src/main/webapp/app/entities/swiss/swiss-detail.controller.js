(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SwissDetailController', SwissDetailController);

    SwissDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Swiss', 'My', 'Combined'];

    function SwissDetailController($scope, $rootScope, $stateParams, previousState, entity, Swiss, My, Combined) {
        var vm = this;

        vm.swiss = entity;
        vm.backLink = function () {
            return My.backLink(previousState);
        };
        My.savePreviousUrl(previousState);
        
        if(vm.swiss.inCombined){
            vm.combined = Combined.findByTournament(vm.swiss); 
        }
        
        vm.isSwiss = true; //for game dialog

        var unsubscribe = $rootScope.$on('tournamentControlApp:swissUpdate', function(event, result) {
            vm.swiss = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        function allGamesFinished() {
            for (var i = 0; i < vm.swiss.matches.length; i++) {
                if(!vm.swiss.matches[i].finished){
                    return false;
                }
            }
            return true;
        }
        
        vm.cannotGenerateNextRound = function () {
            if(vm.swiss.roundsToGenerate === 0){
                return true;
            }
            return !allGamesFinished();
        };
        vm.isGeneratingNextRound = false;
        
        vm.generateNextRound = function () {
            vm.isGeneratingNextRound = true;
            Swiss.generateNextRound(vm.swiss, onGenerateNextRoundSuccess, onGenerateNextRoundError);
        };
        
        function onGenerateNextRoundSuccess(result) {
            $scope.$emit('tournamentControlApp:swissUpdate', result);
            vm.isGeneratingNextRound = false;
            //$window.location.reload();
        }
        
        function onGenerateNextRoundError(error) {
            console.log('generateNextRound() - error occured: ', error.data.message);
            vm.isGeneratingNextRound = false;
        }
        
        vm.nextRoundGenerated = function (round) {
            return round < (vm.swiss.rounds - vm.swiss.roundsToGenerate);
        };
        
        $('#generateNextRoundBtn').on('click', function () {
            var $btn = $(this).button('loading');
            // business logic...
            //$btn.button('reset');
        });
        
        vm.back = function () {
            window.history.back();
        };
    }
})();
