(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedDetailController', CombinedDetailController);

    CombinedDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Combined', 'Participant', 'Tournament', 'User', 'My'];

    function CombinedDetailController($scope, $rootScope, $stateParams, previousState, entity, Combined, Participant, Tournament, User, My) {
        var vm = this;

        vm.combined = entity;
        
        My.savePreviousUrl(previousState);
        vm.backLink = function () {
            return My.backLink(previousState);
        };
        
        vm.generatePlayoff = generatePlayoff;
        vm.isGeneratingPlayoff = false;

        var unsubscribe = $rootScope.$on('tournamentControlApp:combinedUpdate', function(event, result) {
            vm.combined = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        vm.isElimination = function(type){
            return type === 'ELIMINATION_SINGLE' || type === 'ELIMINATION_DOUBLE';
        };
        vm.tournamentIsFinished = function (tournament) {
            if(!tournament) return false;
            if(tournament.tournamentType === 'SWISS' 
                    && tournament.roundsToGenerate 
                    && tournament.roundsToGenerate !== 0){
                return false;
                
            }
            for (var m = 0; m < tournament.matches.length; m++) {
                var match = tournament.matches[m];
                if(!match.finished){
                    return false;
                }
            }
            return true;
        };
        
        vm.getLink = My.getTournamentLink;
        /**
         * @returns true if all group tournaments are finished, false otherwise
         */
        vm.cannotGeneratePlayoff = function () {
            if(!vm.combined.groups) return false; //shouldn't happen, groups at least 1
            for (var g = 0; g < vm.combined.groups.length; g++) {
                var group = vm.combined.groups[g];
                if(!vm.tournamentIsFinished(group)){
                    return true;
                }
            }
            return false;
        };
        
        function generatePlayoff() {
            if(vm.isGeneratingPlayoff || vm.cannotGeneratePlayoff())return;
            vm.isGeneratingPlayoff = true;
            Combined.generatePlayoff(vm.combined.id, onSuccess, onError);
        }
        
        function onSuccess(result) {
            $scope.$emit('tournamentControlApp:combinedUpdate', result);
            vm.isGeneratingPlayoff = false;
        }
        
        function onError() {
            vm.isGeneratingPlayoff = false;
        }
        
        vm.playoffNotGenerated = function () {
            if(!vm.combined.playoff 
                    || !vm.combined.playoff.participants
                    || vm.combined.playoff.participants.length < 2 ){
                return true;
            }
            return false;
        };
        
        
        $('#popoverWrap').popover({
            trigger: 'hover',
            content: "<span data-translate=''>All game tournaments have to be finished to generate playoff.</span>",
            placement: "right",
            html: true,
            container: 'body'
        });
        
        
    }
})();
