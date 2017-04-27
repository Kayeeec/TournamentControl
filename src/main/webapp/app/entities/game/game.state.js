(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('game', {
            parent: 'entity',
            url: '/game',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.game.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/game/games.html',
                    controller: 'GameController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('game');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('game-detail', {
            parent: 'all-versus-all-detail',
            url: '/game-detail/{gameId}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.game.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/game/game-detail.html',
                    controller: 'GameDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('game');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Game', function($stateParams, Game) {
                    return Game.get({id : $stateParams.gameId}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'all-versus-all-detail',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('game-detail.edit', {
            parent: 'game-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/game/game-dialog.html',
                    controller: 'GameDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Game', function(Game) {
                            return Game.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('game.new', {
            parent: 'game',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/game/game-dialog.html',
                    controller: 'GameDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                finished: null,
                                round: null,
                                period: null,
                                note: null,
                                playingField: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('game', null, { reload: 'game' });
                }, function() {
                    $state.go('game');
                });
            }]
        })
        .state('game.edit', {
            parent: 'all-versus-all-detail',
            url: '/game-edit/{gameId}',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/game/game-dialog.html',
                    controller: 'GameDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Game', function(Game) {
                                console.log("gameId is "+ $stateParams.gameId);
                            return Game.get({id : $stateParams.gameId}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('all-versus-all-detail', null, { reload: 'all-versus-all-detail' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('game.delete', {
            parent: 'game',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/game/game-delete-dialog.html',
                    controller: 'GameDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Game', function(Game) {
                            return Game.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('game', null, { reload: 'game' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
