(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('PlayerDeleteController',PlayerDeleteController);

    PlayerDeleteController.$inject = ['$uibModalInstance', 'entity', 'Player'];

    function PlayerDeleteController($uibModalInstance, entity, Player) {
        var vm = this;

        vm.player = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        vm.participantInTournamentError = false;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
//            Player.delete({id: id}, 
//                function () {
//                    $uibModalInstance.close(true);
//                });
              Player.delete({id: id}, onDeleteSuccess, onDeleteError);
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
