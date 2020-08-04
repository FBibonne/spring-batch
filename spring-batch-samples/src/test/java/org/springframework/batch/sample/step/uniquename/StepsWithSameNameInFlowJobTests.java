package org.springframework.batch.sample.step.uniquename;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class StepsWithSameNameInFlowJobTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Test
	public void testFlow() throws Exception {

		jobLauncherTestUtils.launchJob();

		/*
		 * new DefaultJobParametersConverter().getJobParameters(PropertiesConverter.stringToProperties(
		 * "run.id(long)=1,parameter=true,run.date=20070122,input.file=classpath:data/fixedLengthImportJob/input/20070122.teststream.ImportTradeDataStep.txt"
		 * )));
		 */
		String resultat = TaskForFlowTests.getSms();
		System.out.println(resultat);
		assertTrue(resultat.equals("S1_step1ODDS2_step2") || resultat.equals("S1_step1EVENS3_step3S4_step4"));

		// assertEquals(5, after);
	}

	public static class TaskForFlowTests {

		private String nom;

		private static StringBuilder sms = new StringBuilder();

		public static String getSms() {
			return sms.toString();
		}
		
		public void setNom(String nom) {
			this.nom=nom;
		}

		public ExitStatus executeWithParityTest(StepContribution contribution, ChunkContext chunkContext)
				throws Exception {
			String stepName = contribution.getStepExecution().getStepName();
			sms.append(nom+"_"+stepName);
			long horloge=System.currentTimeMillis();
			System.out.println(horloge);
			String exitStatus = (horloge % 2 == 0) ? "EVEN" : "ODD";
			sms.append(exitStatus);
			contribution.setExitStatus(new ExitStatus(exitStatus));
			return new ExitStatus(exitStatus);
		}

		public RepeatStatus executeWithoutParityTest(StepContribution contribution, ChunkContext chunkContext)
				throws Exception {
			String stepName = contribution.getStepExecution().getStepName();
			sms.append(nom+"_"+stepName);
			return RepeatStatus.FINISHED;
		}
	}

}

