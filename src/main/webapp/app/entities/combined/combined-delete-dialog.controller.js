(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedDeleteController',CombinedDeleteController);

    CombinedDeleteController.$inject = ['$uibModalInstance', 'entity', 'Combined'];

    function CombinedDeleteController($uibModalInstance, entity, Combined) {
        var vm = this;

        vm.combined = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Combined.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
