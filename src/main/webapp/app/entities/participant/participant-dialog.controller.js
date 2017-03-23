(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('ParticipantDialogController', ParticipantDialogController);

    ParticipantDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'Participant', 'Player', 'Team', 'User'];

    function ParticipantDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, Participant, Player, Team, User) {
        var vm = this;

        vm.participant = entity;
        vm.clear = clear;
        vm.save = save;
        vm.players = Player.query({filter: 'participant-is-null'});
        $q.all([vm.participant.$promise, vm.players.$promise]).then(function() {
            if (!vm.participant.player || !vm.participant.player.id) {
                return $q.reject();
            }
            return Player.get({id : vm.participant.player.id}).$promise;
        }).then(function(player) {
            vm.players.push(player);
        });
        vm.teams = Team.query({filter: 'participant-is-null'});
        $q.all([vm.participant.$promise, vm.teams.$promise]).then(function() {
            if (!vm.participant.team || !vm.participant.team.id) {
                return $q.reject();
            }
            return Team.get({id : vm.participant.team.id}).$promise;
        }).then(function(team) {
            vm.teams.push(team);
        });
        vm.users = User.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.participant.id !== null) {
                Participant.update(vm.participant, onSaveSuccess, onSaveError);
            } else {
                Participant.save(vm.participant, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:participantUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
