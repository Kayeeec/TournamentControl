(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SwissDeleteController',SwissDeleteController);

    SwissDeleteController.$inject = ['$uibModalInstance', 'entity', 'Swiss'];

    function SwissDeleteController($uibModalInstance, entity, Swiss) {
        var vm = this;

        vm.swiss = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Swiss.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
