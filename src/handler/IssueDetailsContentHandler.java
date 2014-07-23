package handler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import model.TurboComment;
import model.TurboIssue;

import org.eclipse.egit.github.core.Comment;

import service.ServiceManager;
import service.updateservice.CommentUpdateService;

public class IssueDetailsContentHandler {
	private TurboIssue issue;
	
	private ObservableList<Comment> allGhContent = FXCollections.observableArrayList();
	private ObservableList<TurboComment> comments = FXCollections.observableArrayList();
	private ObservableList<TurboComment> log = FXCollections.observableArrayList();
	
	HashMap<Long, String> commentsMarkup = new HashMap<>();
	
	private CommentUpdateService commentsUpdater;
	private ListChangeListener<Comment> commentsChangeListener;
	
	private HashSet<TurboComment> commentsInEditMode = new HashSet<TurboComment>();
	
	public IssueDetailsContentHandler(TurboIssue issue){
		this.issue = issue;
	}
	
	private boolean isNotSetup(){
		return commentsUpdater == null || commentsChangeListener == null;
	}
	
	private void setupContent(){
		//Computationally intensive as list of all comments and their html markup will be retrieved from github
		getDetailsContent();
		commentsUpdater = ServiceManager.getInstance().getCommentUpdateService(issue.getId(), allGhContent);
		setupCommentsChangeListener();
	}
	
	public ObservableList<TurboComment> getComments(){
		return comments;
	}
	
	public ObservableList<TurboComment> getIssueHistory(){
		return log;
	}
	
	public void startContentUpdate(){
		if(isNotSetup()){
			setupContent();
		}
		//TODO:
		if(commentsUpdater != null){
			commentsUpdater.startCommentsListUpdate();			
		}
	}
	
	public void stopContentUpdate(){
		if(commentsUpdater != null){
			commentsUpdater.stopCommentsListUpdate();			
		}
	}
	
	public void toggleCommentEditState(TurboComment comment){
		if(commentIsInEditState(comment)){
			commentsInEditMode.remove(comment);
		}else{
			commentsInEditMode.add(comment);
		}
	}
	
	public void setCommentEditStateFalse(TurboComment comment){
		commentsInEditMode.remove(comment);
	}
	
	public boolean commentIsInEditState(TurboComment comment){
		return commentsInEditMode.contains(comment);
	}
	
	public void createComment(String text){
		try {
			ServiceManager.getInstance().createComment(issue.getId(), text);			
			commentsUpdater.restartCommentsListUpdate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean editComment(TurboComment comment){
		try {
			Comment ghComment = comment.toGhComment();
			ServiceManager.getInstance().editComment(ghComment);
			commentsUpdater.restartCommentsListUpdate();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void deleteComment(TurboComment comment){
		try {
			ServiceManager.getInstance().deleteComment(comment.getId());
			removeCachedItem(comment.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void removeCachedItem(long commentId){
		for(Comment item: allGhContent){
			if(item.getId() == commentId){
				allGhContent.remove(item);
				return;
			}
		}
	}
	
	private void setupCommentsChangeListener(){
		WeakReference<IssueDetailsContentHandler> selfRef = new WeakReference<>(this);
		commentsChangeListener = new ListChangeListener<Comment>(){

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends Comment> arg0) {
				IssueDetailsContentHandler self = selfRef.get();
				if(self != null){
					self.updateData();
				}
			}
		};
		WeakListChangeListener<Comment> listener = new WeakListChangeListener<>(commentsChangeListener);
		allGhContent.addListener(listener);
	}
	
	private void updateData(){
		updateCommentsList();
		updateLogContents();

	}
	
	private void getDetailsContent(){
		try {
			//Reuse allGhContent instance to ensure that all observers get change signals
			allGhContent.clear();
			List<Comment> allItems = ServiceManager.getInstance().getComments(issue.getId());
			allGhContent.addAll(allItems);
			updateData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateCommentsList(){
		List<TurboComment> filteredComments = allGhContent.stream()
												   .map(item -> new TurboComment(item))
												   .collect(Collectors.toList());
		for(TurboComment item : filteredComments){
			updateItemInCommentsList(item);
		}
	}
	
	private void updateItemInCommentsList(TurboComment comment){
		for(TurboComment item : comments){
			if(item.getId() == comment.getId()){
				item.copyValues(comment);
				return;
			}
		}
		addItemToObservedCommentList(comment);
	}
	
	private void updateLogContents(){
		List<TurboComment> logItems = allGhContent.stream()
										   .map(item -> new TurboComment(item))
				   						   .filter(item -> item.isIssueLog())
				   						   .collect(Collectors.toList());
		
		setObservedLog(logItems);
	}
	
	private void setObservedLog(List<TurboComment> logItems){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				log.clear();
				log.addAll(logItems);
			}
		});
	}
	
	private void addItemToObservedCommentList(TurboComment comment){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				comments.add(comment);
			}
		});	
	}
	
	@Override
	public void finalize(){
		commentsUpdater.stopCommentsListUpdate();
	}
}
