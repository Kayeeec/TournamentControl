(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TeamDeleteController',TeamDeleteController);

    TeamDeleteController.$inject = ['$uibModalInstance', 'entity', 'Team'];

    function TeamDeleteController($uibModalInstance, entity, Team) {
        var vm = this;

        vm.team = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        vm.participantInTournamentError = false;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Team.delete({id: id},onDeleteSuccess, onDeleteError);
        }
        function onDeleteSuccess() {
            $uibModalInstance.close(true);
        }
        function onDeleteError(error) {
            console.log('error: ', error);
            if(error.status === 409){
                vm.participantInTournamentError = true;
            }
        }
    }
})();
