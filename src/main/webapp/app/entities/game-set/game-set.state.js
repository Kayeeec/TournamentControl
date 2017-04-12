(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('game-set', {
            parent: 'entity',
            url: '/game-set',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.gameSet.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/game-set/game-sets.html',
                    controller: 'GameSetController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('gameSet');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('game-set-detail', {
            parent: 'game-set',
            url: '/game-set/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.gameSet.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/game-set/game-set-detail.html',
                    controller: 'GameSetDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('gameSet');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'GameSet', function($stateParams, GameSet) {
                    return GameSet.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'game-set',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('game-set-detail.edit', {
            parent: 'game-set-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/game-set/game-set-dialog.html',
                    controller: 'GameSetDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['GameSet', function(GameSet) {
                            return GameSet.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('game-set.new', {
            parent: 'game-set',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/game-set/game-set-dialog.html',
                    controller: 'GameSetDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                scoreA: null,
                                scoreB: null,
                                finished: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('game-set', null, { reload: 'game-set' });
                }, function() {
                    $state.go('game-set');
                });
            }]
        })
        .state('game-set.edit', {
            parent: 'game-set',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/game-set/game-set-dialog.html',
                    controller: 'GameSetDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['GameSet', function(GameSet) {
                            return GameSet.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('game-set', null, { reload: 'game-set' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('game-set.delete', {
            parent: 'game-set',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/game-set/game-set-delete-dialog.html',
                    controller: 'GameSetDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['GameSet', function(GameSet) {
                            return GameSet.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('game-set', null, { reload: 'game-set' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
