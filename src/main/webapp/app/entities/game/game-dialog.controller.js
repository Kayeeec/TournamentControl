(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('GameDialogController', GameDialogController);

    GameDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'Game', 'Participant', 'Tournament'];

    function GameDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, Game, Participant, Tournament) {
        var vm = this;

        vm.game = entity;
        vm.clear = clear;
        vm.save = save;
        vm.rivalas = Participant.query({filter: 'game-is-null'});
        $q.all([vm.game.$promise, vm.rivalas.$promise]).then(function() {
            if (!vm.game.rivalA || !vm.game.rivalA.id) {
                return $q.reject();
            }
            return Participant.get({id : vm.game.rivalA.id}).$promise;
        }).then(function(rivalA) {
            vm.rivalas.push(rivalA);
        });
        vm.rivalbs = Participant.query({filter: 'game-is-null'});
        $q.all([vm.game.$promise, vm.rivalbs.$promise]).then(function() {
            if (!vm.game.rivalB || !vm.game.rivalB.id) {
                return $q.reject();
            }
            return Participant.get({id : vm.game.rivalB.id}).$promise;
        }).then(function(rivalB) {
            vm.rivalbs.push(rivalB);
        });
        vm.tournaments = Tournament.query();

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
