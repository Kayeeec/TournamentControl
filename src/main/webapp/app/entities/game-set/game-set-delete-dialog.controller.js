(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('GameSetDeleteController',GameSetDeleteController);

    GameSetDeleteController.$inject = ['$uibModalInstance', 'entity', 'GameSet'];

    function GameSetDeleteController($uibModalInstance, entity, GameSet) {
        var vm = this;

        vm.gameSet = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            GameSet.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
