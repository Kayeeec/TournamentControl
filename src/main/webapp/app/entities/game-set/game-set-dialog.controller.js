(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('GameSetDialogController', GameSetDialogController);

    GameSetDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'GameSet', 'Game', 'SetSettings'];

    function GameSetDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, GameSet, Game, SetSettings) {
        var vm = this;

        vm.gameSet = entity;
        vm.clear = clear;
        vm.save = save;
        vm.games = Game.query();
        vm.setsettings = SetSettings.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.gameSet.id !== null) {
                GameSet.update(vm.gameSet, onSaveSuccess, onSaveError);
            } else {
                GameSet.save(vm.gameSet, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:gameSetUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
