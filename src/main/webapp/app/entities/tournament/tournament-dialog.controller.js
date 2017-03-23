(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentDialogController', TournamentDialogController);

    TournamentDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Tournament', 'Game', 'User', 'Participant', 'Team', 'Player', '$filter'];

    function TournamentDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Tournament, Game, User, Participant, Team, Player, $filter) {
        var vm = this;

        vm.tournament = entity;
        vm.clear = clear;
        vm.save = save;
        
        vm.games = Game.query(); //should not be needed 
        vm.users = User.query(); //should not be needed
        vm.participants = Participant.query();   
        $scope.participantType = 'player';
        $scope.selectedParticipants;
        

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });
        
        function uncheckAll () {
            $scope.selectedParticipants = [];  
        }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.tournament.participants = $scope.selectedParticipants;
            
            vm.isSaving = true;
            if (vm.tournament.id !== null) {
                Tournament.update(vm.tournament, onSaveSuccess, onSaveError);
            } else {
                Tournament.save(vm.tournament, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:tournamentUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
