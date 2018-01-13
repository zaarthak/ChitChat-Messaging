const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendMessageNotification = functions.database.ref('/Notifications/{friend_user_id}/{my_user_id}/{notification_id}').onWrite(event => {
	
	const friend_user_id = event.params.friend_user_id;
	const my_user_id = event.params.my_user_id;
	const notification_id = event.params.notification_id;
	
	console.log('We have a notification to send to : ', friend_user_id);
	console.log('From : ', my_user_id);
	
	if (!event.data.val()) {

		return console.log('A Notification has been deleted from the database : ', notification_id);
	}
	
	const messageDetails = admin.database().ref(`/Notifications/${friend_user_id}/${my_user_id}/${notification_id}`).once('value');
	
	return messageDetails.then(messageResult => {
		
		const content = messageResult.val().content; 
	
		const userDetails = admin.database().ref(`/Users/${my_user_id}`).once('value');
	
		return userDetails.then(userResult => {
			
			const user_name = userResult.val().username;
			
			const deviceToken = admin.database().ref(`/Users/${friend_user_id}/deviceToken`).once('value');

			return deviceToken.then(result => {

				const token_id = result.val();

				const payload = {
					notification : {
						title : user_name,
						body : content,
						icon : "default",
						click_action : "com.chitchat.messaging.chitchatmessaging_TARGET_NOTIFICATION"
					},
					data : {
						user_id : my_user_id,
						user_name : user_name
					}
				};

				return admin.messaging().sendToDevice(token_id, payload).then(response => {

					console.log('This was the Notification Feature');
				});

			});
		});
		
	});
	
});