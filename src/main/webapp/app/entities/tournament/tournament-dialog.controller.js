(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentDialogController', TournamentDialogController);

    TournamentDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Tournament', 'Game', 'User', 'Participant'];

    function TournamentDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Tournament, Game, User, Participant) {
        var vm = this;

        vm.tournament = entity;
        vm.clear = clear;
        vm.save = save;
        vm.games = Game.query();
        vm.users = User.query();
        vm.participants = Participant.query();
        $scope.player = 1;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
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
