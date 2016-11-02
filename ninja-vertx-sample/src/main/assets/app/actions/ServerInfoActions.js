import alt from '../libs/alt';

class ServerInfoActions {

    serverInfoReceived(message) {
        console.log(message);
        this.dispatch(message);
    }
}

export default alt.createActions(ServerInfoActions);
