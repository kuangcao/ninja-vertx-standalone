import alt from '../libs/alt';

import BusRoutes from '../libs/vertx-bus-routes';

import VertxActions from '../actions/VertxActions';
import ServerInfoActions from '../actions/ServerInfoActions';


class ServerInfoStore {

    constructor()
    {
        var self = this;

        this.state = { serverTime: "-" };

        this.bindListeners({
            handleVertxReady: VertxActions.VERTX_READY,
            handleServerInfoMessage: ServerInfoActions.SERVER_INFO_RECEIVED
        });


    }

    handleServerInfoMessage(message)
    {
        console.log(message);
        this.setState({serverTime: message.systemTime});
    };

    handleVertxReady(eventBus)
    {
        let this_ = this;
     //   this.setState({serverTime: message.systemTime});
        eventBus.registerHandler(
            BusRoutes.server_info,
            function(error,message){
                console.log( message.body);
                this_.setState({serverTime: message.body.result.name});
            }
        );
    }
}

export default alt.createStore(ServerInfoStore, 'ServerInfoStore');