(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('set-settings', {
            parent: 'entity',
            url: '/set-settings',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.setSettings.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/set-settings/set-settings.html',
                    controller: 'SetSettingsController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('setSettings');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('set-settings-detail', {
            parent: 'set-settings',
            url: '/set-settings/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.setSettings.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/set-settings/set-settings-detail.html',
                    controller: 'SetSettingsDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('setSettings');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'SetSettings', function($stateParams, SetSettings) {
                    return SetSettings.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'set-settings',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('set-settings-detail.edit', {
            parent: 'set-settings-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/set-settings/set-settings-dialog.html',
                    controller: 'SetSettingsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['SetSettings', function(SetSettings) {
                            return SetSettings.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('set-settings.new', {
            parent: 'set-settings',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/set-settings/set-settings-dialog.html',
                    controller: 'SetSettingsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                maxScore: null,
                                minReachedScore: null,
                                leadByPoints: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('set-settings', null, { reload: 'set-settings' });
                }, function() {
                    $state.go('set-settings');
                });
            }]
        })
        .state('set-settings.edit', {
            parent: 'set-settings',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/set-settings/set-settings-dialog.html',
                    controller: 'SetSettingsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['SetSettings', function(SetSettings) {
                            return SetSettings.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('set-settings', null, { reload: 'set-settings' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('set-settings.delete', {
            parent: 'set-settings',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/set-settings/set-settings-delete-dialog.html',
                    controller: 'SetSettingsDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['SetSettings', function(SetSettings) {
                            return SetSettings.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('set-settings', null, { reload: 'set-settings' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
