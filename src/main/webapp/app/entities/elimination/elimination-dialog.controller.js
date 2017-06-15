(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EliminationDialogController', EliminationDialogController);

    EliminationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Elimination', 'Participant', 'filterFilter'];

    function EliminationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Elimination, Participant, filterFilter) {
        var vm = this;

        vm.elimination = entity;
        
        vm.clear = clear;
        vm.save = save;
        
         vm.participants = Participant.query();
        
//        initiating default values 
        vm.elimination.bronzeMatch = vm.elimination.bronzeMatch || false;
        vm.elimination.type = vm.elimination.type || "SINGLE";
        vm.elimination.tiesAllowed = false;
        
        /* participants stuff */
        vm.elimination.participants = vm.elimination.participants || [];
        vm.selectedPlayers = filterFilter(vm.elimination.participants, {team : null});
        vm.selectedTeams = filterFilter(vm.elimination.participants, {player : null});
        $scope.chosen = 1;
        
        function selectParticipants() {
            console.log("Selecting participants");
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.elimination.participants);
                console.log("vm.selectedPlayers: " + vm.selectedPlayers);
                console.log("vm.elimination.participants: " + vm.elimination.participants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.elimination.participants);
                console.log("vm.selectedTeams: " + vm.selectedTeams);
                console.log("vm.elimination.participants: " + vm.elimination.participants);
            }
        }
        $scope.isPlayer = function (participant) {
            if(participant.player !== null) return true;
            return false;
        };
        $scope.isTeam = function (participant) {
            if(participant.team !== null) return true;
            return false;
        };
        /* END - participants stuff */
        
        /* *** participant seeding stuff *** */
        $scope.seedRandomly = true;
        vm.myToogle = function (boolValue) {
            $scope.seedRandomly = boolValue;
        };
        
        /* *** participant seeding stuff END *** */
        
         /* setSettings stuff */
        vm.setSettingsChosen = initSetSettingsChosen();
        vm.preparedSettings = vm.elimination.setSettings 
                || {id: null, maxScore: null, leadByPoints: null, minReachedPoints: null};
        
        function initSetSettingsChosen() {
            if (vm.elimination.id !== null && vm.elimination.setSettings !== null) {
                if(vm.elimination.setSettings.leadByPoints !== null 
                    || vm.elimination.setSettings.minReachedScore !== null){
                    return 'leadByPoints';
                }
                return 'maxScore';
            }
            return null;
        }
        
        function resolveSetSettings() {
            if (vm.setSettingsChosen === 'maxScore') {
                vm.preparedSettings.leadByPoints = null;
                vm.preparedSettings.minReachedScore = null;
            }
            if (vm.setSettingsChosen === 'leadByPoints') {
                vm.preparedSettings.maxScore = null;
            }
            if (vm.preparedSettings.id !== null ||
                vm.preparedSettings.maxScore !== null ||
                vm.preparedSettings.leadByPoints !== null ||
                vm.preparedSettings.minReachedScore !== null){
                    vm.elimination.setSettings = vm.preparedSettings;
                    console.log(vm.elimination.setSettings);
            }
        }
        /* END - setSettings stuff */
        
        $('#myTab a[href="#players"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        $('#myTab a[href="#teams"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        
        $('.collapse').collapse();
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            
            selectParticipants();
            resolveSetSettings();
            
            if (vm.elimination.id !== null) {
                Elimination.update(vm.elimination, onSaveSuccess, onSaveError);
            } else {
                Elimination.save(vm.elimination, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:eliminationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
