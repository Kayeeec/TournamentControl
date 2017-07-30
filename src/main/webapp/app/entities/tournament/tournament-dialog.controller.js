(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentDialogController', TournamentDialogController);

    TournamentDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'Tournament', 'Game', 'User', 'Participant', 'SetSettings'];

    function TournamentDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, Tournament, Game, User, Participant, SetSettings) {
        var vm = this;

        vm.tournament = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.games = Game.query();
        vm.users = User.query();
        vm.participants = Participant.query();
        $scope.player = 1;
        
        vm.setsettings = SetSettings.query({filter: 'tournament-is-null'});
        $q.all([vm.tournament.$promise, vm.setsettings.$promise]).then(function() {
            if (!vm.tournament.setSettings || !vm.tournament.setSettings.id) {
                return $q.reject();
            }
            return SetSettings.get({id : vm.tournament.setSettings.id}).$promise;
        }).then(function(setSettings) {
            vm.setsettings.push(setSettings);
        });

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

        vm.datePickerOpenStatus.created = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
