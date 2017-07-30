(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SetSettingsDetailController', SetSettingsDetailController);

    SetSettingsDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'SetSettings'];

    function SetSettingsDetailController($scope, $rootScope, $stateParams, previousState, entity, SetSettings) {
        var vm = this;

        vm.setSettings = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:setSettingsUpdate', function(event, result) {
            vm.setSettings = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
