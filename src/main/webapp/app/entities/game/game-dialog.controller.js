(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .controller('GameDialogController', GameDialogController);

    GameDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Game', 'Tournament', 'Participant', 'GameSet'];

    function GameDialogController($timeout, $scope, $stateParams, $uibModalInstance, entity, Game, Tournament, Participant, GameSet) {
        var vm = this;

        vm.game = entity;
        vm.clear = clear;
        vm.save = save;
        vm.addSet = addSet;
//        vm.tournaments = Tournament.query();
//        vm.participants = Participant.query();
        vm.gamesets = GameSet.query();

        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            console.log("it calls save");
            vm.isSaving = true;
            
            if (vm.game.id !== null) {
                //update all sets
                console.log(vm.game.sets);
                for (var i = 0; i < vm.game.sets.length; i++) {
                    var set = vm.game.sets[i];
                    console.log(set);
                    if (set.id !== null) {
                        GameSet.update(set);
                    } else {
                        GameSet.save(set);
                    }
                }
                
                
                Game.update(vm.game, onSaveSuccess, onSaveError);
            } else {
                Game.save(vm.game, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('tournamentControlApp:gameUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }

        function addSet() {
            console.log("im here " + vm.game.id);
            Game.addSet({id: vm.game.id}, function (result) {
                vm.game = result;
            });
        }

        vm.removeSet = function (setId) {
            console.log("im here " + setId);
            Game.removeSet({id: setId}, function (result) {
                vm.game = result;
            });
        };


    }
})();
