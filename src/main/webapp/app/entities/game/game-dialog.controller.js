(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('GameDialogController', GameDialogController);

    GameDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Game', 'Tournament', 'Participant', 'GameSet'];

    function GameDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Game, Tournament, Participant, GameSet) {
        var vm = this;

        vm.game = entity;
        vm.clear = clear;
        vm.save = save;
        vm.tournaments = Tournament.query();
        vm.participants = Participant.query();
        vm.gamesets = GameSet.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.game.id !== null) {
                Game.update(vm.game, onSaveSuccess, onSaveError);
            } else {
                Game.save(vm.game, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:gameUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
