(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('ParticipantDeleteController',ParticipantDeleteController);

    ParticipantDeleteController.$inject = ['$uibModalInstance', 'entity', 'Participant'];

    function ParticipantDeleteController($uibModalInstance, entity, Participant) {
        var vm = this;

        vm.participant = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Participant.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
