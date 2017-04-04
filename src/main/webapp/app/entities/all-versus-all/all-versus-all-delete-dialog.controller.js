(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('AllVersusAllDeleteController',AllVersusAllDeleteController);

    AllVersusAllDeleteController.$inject = ['$uibModalInstance', 'entity', 'AllVersusAll'];

    function AllVersusAllDeleteController($uibModalInstance, entity, AllVersusAll) {
        var vm = this;

        vm.allVersusAll = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            AllVersusAll.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
