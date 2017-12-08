(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedDetailController', CombinedDetailController);

    CombinedDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Combined', 'Participant', 'Tournament', 'User'];

    function CombinedDetailController($scope, $rootScope, $stateParams, previousState, entity, Combined, Participant, Tournament, User) {
        var vm = this;

        vm.combined = entity;
        vm.previousState = previousState.name;
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
            if(tournament.type === 'swiss' 
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
        
        vm.getLink = function (tournament) {
            switch (tournament.tournamentType) {
                case 'allVersusAll':
                    return "all-versus-all-detail({id:"+tournament.id+"})";
                case 'swiss':
                    return "swiss-detail({id:"+tournament.id+"})";
                default:
                    return "elimination-detail({id:"+tournament.id+"})";
            }
        };
        
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
            Combined.generatePlayoff({id : vm.combined.id}, onSuccess, onError);
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
