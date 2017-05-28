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
        vm.changed = false;
        
        vm.chosenMap = new Map(); //determines which setSettings are used (radio 'MaxScore 'or 'Lead-by points')
        vm.oldSettings = new Map();
        vm.finishGameDisabledCause = null;
        
        //initialize chosenMap
        for (var i = 0; i < vm.game.sets.length; i++) {
            var set = vm.game.sets[i];
            if(set.setSettings.maxScore !== null){
                vm.chosenMap[set.id]="maxScore";
            }
            if(set.setSettings.leadByPoints !== null) {
                vm.chosenMap[set.id]="leadByPoints";
            }
        }

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

        /* ************* Functionality ************* */

        vm.addSet = function () {
            vm.changed = true;
            Game.addSet({id: vm.game.id}, function (result) {
                vm.game = result;
            });
        };

        vm.updateSetSettings = function (set) {
            vm.changed = true;
            delete vm.oldSettings[set.id];

            if (vm.chosenMap[set.id] === "maxScore") {
                set.setSettings.leadByPoints = null;
                set.setSettings.minReachedScore = null;
            }
            if (vm.chosenMap[set.id] === "leadByPoints") {
                set.setSettings.maxScore = null;
            }

            Game.updateSetSettings(set, function (result) {
                vm.game = result;
            });
        };
        
        vm.cancelSetSettings = function (updatedSet, index) {
            vm.game.sets[index].setSettings = angular.copy(vm.oldSettings[updatedSet.id]);
        };
        
        vm.copyToOldSettings = function (set) {
            vm.oldSettings[set.id] = angular.copy(set.setSettings);
        };

        vm.removeSet = function (setId) {
            vm.changed = true;
            Game.removeSet({id: setId}, function (result) {
                vm.game = result;
            });
        };

        $('.collapse').collapse();

        /* ************* Logic ************* */

        function saveSet(set) {
            console.log("saveSet() called");
            vm.changed = true;
            GameSet.update(set);
        }
        
        function allSetsFinished_and_setsToWin_notReached() {
            var winCounter = {A: 0, B: 0};
            for (var i = 0; i < vm.game.sets.length; i++) {
                if (vm.game.sets[i].finished === false) {
                    return false;
                }
                if (vm.game.sets[i].scoreA > vm.game.sets[i].scoreB) {
                    winCounter.A += 1;
                }
                if (vm.game.sets[i].scoreB > vm.game.sets[i].scoreA) {
                    winCounter.B += 1;
                }
            }

            if (winCounter.A < vm.game.tournament.setsToWin && winCounter.B < vm.game.tournament.setsToWin) {
                return true;
            }
            return false;
        }

        vm.save_CheckForSetToBeAdded = function (set) {
            saveSet(set);
            if (set.finished && !vm.game.tournament.tiesAllowed) {
                if (allSetsFinished_and_setsToWin_notReached()) {
                    addSet();
                }
            }
        };

        vm.save_shouldFinishSetCheck = function (set, changedScore, otherScore) {
            saveSet(set);
            if (set.setSettings.maxScore !== null) {
                if (set[changedScore] >= set.setSettings.maxScore) {
                    set.finished = true;
                    vm.save_CheckForSetToBeAdded(set);
                }
            } 
            if(set.setSettings.leadByPoints !== null){
                if (set[changedScore] >= set.setSettings.minReachedScore
                        && set[changedScore] >= set[otherScore] + set.setSettings.leadByPoints) {
                    set.finished = true;
                    vm.save_CheckForSetToBeAdded(set);
                }
            }
        };
        
        vm.unfinishedSetPresent_orGameIsTie = function () {
            var winsA = 0;
            var winsB = 0;
            
            for (var i = 0; i < vm.game.sets.length; i++) {
                var set = vm.game.sets[i];
                if(!set.finished){ vm.finishGameDisabledCause = "unfinished"; return true;}
                if(set.scoreA > set.scoreB) {winsA += 1;}
                if(set.scoreB > set.scoreA) {winsB += 1;}
            }
            
            if(!vm.game.tournament.tiesAllowed){
                if((vm.game.tournament.setsToWin !== null 
                    && winsA < vm.game.tournament.setsToWin 
                    && winsB < vm.game.tournament.setsToWin)
                    || winsA === winsB){
                        vm.finishGameDisabledCause = "tie";
                        return true;
                }
            }
            return false;
        };


    }
})();
