(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .controller('GameDialogController', GameDialogController);

    GameDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Game', 'Tournament', 'Participant', 'GameSet', 'My', 'SetSettings'];

    function GameDialogController($timeout, $scope, $stateParams, $uibModalInstance, entity, Game, Tournament, Participant, GameSet, My, SetSettings) {
        var vm = this;

        vm.game = entity;
        vm.clear = clear;
        vm.save = save;
        
        vm.finishGameDisabledCause = null;
        
        vm.getName = My.getParticipantName;

        /* ************* Functionality ************* */
        
        //vm.maxSetId = 0;
        vm.newSetId = 'A';
        
        function initPreparedSets_andNewSetId() {
            var sets = new Array();
            for (var i = 0; i < vm.game.sets.length; i++) {
//                var setCopy = angular.copy(vm.game.sets[i]);
//                sets.push(setCopy);
                sets.push(vm.game.sets[i]);
                //noting maximal id 
                //vm.maxSetId = Math.max(vm.maxSetId, vm.game.sets[i].id);
            }
            return sets;
        }
        vm.preparedSets = initPreparedSets_andNewSetId();
        
        function nextChar(c) {
            if(c === "Z") return "A";
            return String.fromCharCode(c.charCodeAt(0) + 1);
        }
        
        vm.addSet = function () {
            var setToAdd = { id: vm.newSetId,
                        finished: false,
                        scoreA: 0,
                        scoreB: 0,
                        setSettings: vm.game.tournament.setSettings
                    };
            vm.setSettingsMap.set(vm.newSetId, {chosen: determineInitialChosen(setToAdd), 
                    oldSetSettings: angular.copy(setToAdd.setSettings), 
                    newSetSettings: angular.copy(setToAdd.setSettings)
                    });
            vm.newSetId = nextChar(vm.newSetId);
            vm.preparedSets.push(setToAdd);
        };
        
        vm.removeSetById = function (setId) {
            for (var i = 0; i < vm.preparedSets.length; i++) {
                if(vm.preparedSets[i].id === setId){
                    vm.preparedSets.splice(i, 1);
                    break;
                }
            }
            vm.setSettingsMap.delete(setId);
        };
        
        function determineInitialChosen(set) {
            if(set && set.setSettings && 
                    (set.setSettings.leadByPoints || set.setSettings.minReachedScore)){
                return 'leadByPoints';
            }
            return 'maxScore';
        }
        
        function initSetSettingsMap() {
            var map = new Map();
            for (var i = 0; i < vm.preparedSets.length; i++) {
                map.set(vm.preparedSets[i].id, {chosen: determineInitialChosen(vm.preparedSets[i]), 
                    oldSetSettings: angular.copy(vm.preparedSets[i].setSettings || vm.game.tournament.setSettings), 
                    newSetSettings: angular.copy(vm.preparedSets[i].setSettings || vm.game.tournament.setSettings)
                    }
                );
            }
            return map;
        }
        
        /**
         * [ [key, value], [key, value] ... ]
         * 
         * key: set id
         * value: { chosen: 'what radio is chosen, either maxScore or leadByPoints',
         *          oldSetSettings: {}, 
         *          newSetSettings: {}  <-form mapped to this
         *        }
         */
        vm.setSettingsMap = initSetSettingsMap();
        
        vm.updateSetSettings = function (set) {
            if(vm.setSettingsMap.get(set.id).newSetSettings.id === vm.game.tournament.setSettings.id){
                vm.setSettingsMap.get(set.id).newSetSettings.id = null;
            }
            var newss = JSON.parse(JSON.stringify(vm.setSettingsMap.get(set.id).newSetSettings));
            
            if(vm.setSettingsMap.get(set.id).chosen === "maxScore"){
                newss.leadByPoints = null;
                newss.minReachedScore = null;
            }else {
                newss.maxScore = null;
            }
            
            set.setSettings = angular.copy(newss);
            vm.setSettingsMap.get(set.id).oldSetSettings = angular.copy(newss);
        };
        
        vm.resetSetSettings = function (set) {
            vm.setSettingsMap.get(set.id).newSetSettings = angular.copy(vm.setSettingsMap.get(set.id).oldSetSettings);
        };

        $('.collapse').collapse();

        /* ************* Logic ************* */

        
        function allSetsFinished_and_setsToWin_notReached() {
            var winCounter = {A: 0, B: 0};
            for (var i = 0; i < vm.preparedSets.length; i++) {
                if (vm.preparedSets[i].finished === false) {
                    return false;
                }
                if (vm.preparedSets[i].scoreA > vm.preparedSets[i].scoreB) {
                    winCounter.A += 1;
                }
                if (vm.preparedSets[i].scoreB > vm.preparedSets[i].scoreA) {
                    winCounter.B += 1;
                }
            }

            if (winCounter.A < vm.game.tournament.setsToWin && winCounter.B < vm.game.tournament.setsToWin) {
                return true;
            }
            return false;
        }

        vm.save_CheckForSetToBeAdded = function (set) {
            if(!set.finished){
                vm.game.finished = false;
            }
            if (allSetsFinished_and_setsToWin_notReached() && !vm.game.tournament.tiesAllowed) {
                vm.addSet();
            }
        };

        vm.save_shouldFinishSetCheck = function (set, changedScore, otherScore) {
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
                    vm.save_CheckForSetToBeAdded(set); //run on ng-change therfore run twice
                }
            }
        };
        
        vm.unfinishedSetPresent_orGameIsTie = function () {
            var winsA = 0;
            var winsB = 0;
            
            for (var i = 0; i < vm.preparedSets.length; i++) {
                var set = vm.preparedSets[i];
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
        
        
        //====================================================
        
        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
        
        function removeNewIds() {
            for (var i = 0; i < vm.preparedSets.length; i++) {
                if(typeof  vm.preparedSets[i].id === 'string'){
                    vm.preparedSets[i].id = null;
                }
            }
        }

        function save() {
            console.log("it calls save");
            vm.isSaving = true;
            
            removeNewIds();
            vm.game.sets = vm.preparedSets;
                        
            if (vm.game.id !== null) {
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


    }
})();
